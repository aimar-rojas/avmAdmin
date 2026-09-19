package aimar.rojas.avmadmin.features.accounting.domain

/**
 * Señala que el backend reconoció el comprobante como un registro existente.
 * La UI usa el tipo, no el detalle del servidor, para mostrar una explicación clara.
 */
class DuplicateExpenseInvoiceException : Exception()
