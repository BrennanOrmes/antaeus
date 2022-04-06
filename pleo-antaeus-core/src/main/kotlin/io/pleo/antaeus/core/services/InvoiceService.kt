/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    /**
     * @param status the status to fetch as [String]
     * @throws IllegalArgumentException
     */
    fun fetchAllByStatus(status: String): List<Invoice> {
        val statusValue = InvoiceStatus.valueOf(status)
        // Return invoices by status
        return fetchAll().filter { it.status == statusValue }
    }
}
