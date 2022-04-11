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
     * Gets invoices by status
     * @param status the status to fetch as [String]
     * @return filtered invoice as [List] of type [Invoice]
     * @throws IllegalArgumentException
     */
    fun fetchAllByStatus(status: String): List<Invoice> {
        val statusValue = InvoiceStatus.valueOf(status)
        // Return invoices by status
        return fetchAll().filter { it.status == statusValue }
    }

    /**
     * Updates invoices status
     * @param invoiceId to be passed to [dal] as [Int]
     * @param status to be passed to [dal] as [String]
     * @return invoice from [dal] as [Invoice]
     * @throws InvoiceNotFoundException
     * @throws IllegalArgumentException
     */
    fun updateInvoice(invoiceId: Int, status: String): Invoice {
        val statusValue = InvoiceStatus.valueOf(status)
        return dal.updateInvoiceStatus(invoiceId, statusValue) ?: throw InvoiceNotFoundException(invoiceId)
    }
}
