package aimar.rojas.avmadmin.core.workers

import aimar.rojas.avmadmin.features.parties.data.local.PartyDao
import aimar.rojas.avmadmin.features.parties.data.PartiesApiService
import aimar.rojas.avmadmin.features.parties.data.CreatePartyRequest

import aimar.rojas.avmadmin.features.shipments.data.local.ShipmentDao
import aimar.rojas.avmadmin.features.shipments.data.ShipmentsApiService
import aimar.rojas.avmadmin.features.shipments.data.CreateShipmentRequest

import aimar.rojas.avmadmin.features.trades.data.local.TradeDao
import aimar.rojas.avmadmin.features.trades.data.TradesApiService
import aimar.rojas.avmadmin.features.trades.data.CreateTradeRequest

import aimar.rojas.avmadmin.core.data.local.dao.IdMappingDao
import aimar.rojas.avmadmin.core.data.local.entities.IdMappingEntity

import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import aimar.rojas.avmadmin.features.selections.data.local.SelectionDao

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@HiltWorker
class AvmSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val partyDao: PartyDao,
    private val partyApi: PartiesApiService,
    private val shipmentDao: ShipmentDao,
    private val shipmentApi: ShipmentsApiService,
    private val tradeDao: TradeDao,
    private val tradeApi: TradesApiService,
    private val selectionsRepo: SelectionsRepository,
    private val selectionDao: SelectionDao,
    private val sessionDataStore: aimar.rojas.avmadmin.data.local.SessionDataStore,
    private val idMappingDao: IdMappingDao
) : CoroutineWorker(appContext, workerParams) {

    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d("AvmAdminSync", "AvmSyncWorker: Starting doWork() execution...")

            syncParties()
            syncShipments()
            syncTrades()
            syncSelections()

            Log.d("AvmAdminSync", "AvmSyncWorker: Sync process completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("AvmAdminSync", "AvmSyncWorker: Sync failed with exception", e)
            Result.retry()
        }
    }

    private suspend fun syncParties() {
        val pending = partyDao.getPendingSyncParties()
        for (local in pending) {
            if (local.syncOperation == "CREATE" || local.partyId <= 0) {
                val req = CreatePartyRequest(
                    partyRole = local.partyRole,
                    aliasName = local.aliasName,
                    firstName = local.firstName,
                    lastName = local.lastName,
                    dni = local.dni,
                    ruc = local.ruc,
                    phone = local.phone
                )
                val res = partyApi.createParty(req)
                if (res.isSuccessful && res.body() != null) {
                    val remoteId = res.body()!!.party.partyId
                    idMappingDao.insertMapping(IdMappingEntity(entityType = "PARTY", oldId = local.partyId, newId = remoteId))
                    tradeDao.updateForeignPartyId(local.partyId, remoteId)
                    partyDao.updatePartyIdAndMarkSynced(local.partyId, remoteId) 
                } else {
                    throw Exception("Failed to sync Party ${local.partyId}")
                }
            }
        }
    }

    private suspend fun syncShipments() {
        val pending = shipmentDao.getPendingSyncShipments()
        for (local in pending) {
            if (local.syncOperation == "CREATE" || local.shipmentId <= 0) {
                val req = CreateShipmentRequest(
                    startDate = dateOnlyFormat.format(local.startDate),
                    endDate = local.endDate?.let { dateOnlyFormat.format(it) },
                    status = local.status
                )
                val res = shipmentApi.createShipment(req)
                if (res.isSuccessful && res.body() != null) {
                    val remoteId = res.body()!!.shipment.shipmentId
                    idMappingDao.insertMapping(IdMappingEntity(entityType = "SHIPMENT", oldId = local.shipmentId, newId = remoteId))
                    tradeDao.updateForeignShipmentId(local.shipmentId, remoteId)
                    shipmentDao.updateShipmentIdAndMarkSynced(local.shipmentId, remoteId)
                } else {
                    throw Exception("Failed to sync Shipment ${local.shipmentId}")
                }
            }
        }
    }

    private suspend fun syncTrades() {
        val pending = tradeDao.getPendingSyncTrades()
        for (local in pending) {
            if (local.syncOperation == "CREATE" || local.tradeId <= 0) {
                val req = CreateTradeRequest(
                    partyId = local.partyId,
                    shipmentId = local.shipmentId,
                    tradeType = local.tradeType,
                    startDatetime = local.startDatetime,
                    endDatetime = local.endDatetime?.takeIf { it.isNotEmpty() },
                    discountWeightPerTray = local.discountWeightPerTray,
                    varietyAvocado = local.varietyAvocado
                )
                val res = tradeApi.createTrade(req)
                if (res.isSuccessful && res.body() != null) {
                    val remoteId = res.body()!!.trade.tradeId
                    idMappingDao.insertMapping(IdMappingEntity(entityType = "TRADE", oldId = local.tradeId, newId = remoteId))
                    selectionDao.updateForeignTradeId(local.tradeId, remoteId)
                    tradeDao.updateTradeIdAndMarkSynced(local.tradeId, remoteId)
                    sessionDataStore.emitTradeIdMapping(local.tradeId, remoteId)
                    Log.d("AvmAdminSync", "AvmSyncWorker: Successfully promoted Trade ${local.tradeId} to API ID $remoteId")
                } else {
                    throw Exception("Failed to sync Trade ${local.tradeId}")
                }
            }
        }
    }

    private suspend fun syncSelections() {
        val tradeIds = selectionsRepo.getPendingSyncTradeIdsList()
        for (tradeId in tradeIds) {
            if (tradeId > 0) { 
                selectionsRepo.syncAllSelectionsForTrade(tradeId)
            }
        }
    }
}
