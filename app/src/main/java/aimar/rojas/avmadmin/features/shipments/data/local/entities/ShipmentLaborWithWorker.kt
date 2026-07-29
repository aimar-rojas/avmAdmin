package aimar.rojas.avmadmin.features.shipments.data.local.entities

import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentLabor
import aimar.rojas.avmadmin.features.workers.data.local.entities.WorkerEntity
import androidx.room.Embedded
import androidx.room.Relation

data class ShipmentLaborWithWorker(
    @Embedded val labor: ShipmentLaborEntity,
    @Relation(
        parentColumn = "workerLocalId",
        entityColumn = "localId"
    )
    val worker: WorkerEntity
) {
    fun toDomain(): ShipmentLabor {
        return ShipmentLabor(
            laborId = labor.localId,
            remoteId = labor.remoteId,
            shipmentId = labor.shipmentLocalId,
            workerId = labor.workerLocalId,
            workerName = worker.fullName,
            workDate = labor.workDate,
            amount = labor.amount,
            notes = labor.notes,
            syncState = labor.syncState,
            syncError = labor.syncError
        )
    }
}
