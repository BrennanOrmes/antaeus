package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    /**
     * Charge invoice using [PaymentProvider]
     * @param invoice as [Invoice]
     * @return updated invoice as [Invoice]
     * @throws IllegalArgumentException
     * @throws InvoiceNotFoundException
     */
    fun chargeInvoice(invoice: Invoice): Invoice {
        val status: InvoiceStatus = try {
            if (paymentProvider.charge(invoice)) {
                InvoiceStatus.PAID
            } else {
                InvoiceStatus.FAILED
            }
        } catch (exception: CurrencyMismatchException) {
            InvoiceStatus.FAILED
        } catch (exception: CustomerNotFoundException) {
            InvoiceStatus.FAILED
        } catch (exception: NetworkException) {
            InvoiceStatus.PENDING
        }

        return invoiceService.updateInvoice(invoice.id, status.toString())
    }

    // method to be used for cron jobs
    fun batch() {
        var retry = 0
        var invoices = invoiceService.fetchAllByStatus(InvoiceStatus.PENDING.toString())

        while (hasPendingInvoices(invoices) && retry <= 3) {
            retry += 1
            invoices = chargeInvoices(invoices)
        }

        if (hasPendingInvoices(invoices)) {
            // set them to failed in invoices
            setPendingInvoicesToFailed(invoices)
        }
    }

    private fun chargeInvoices(invoices: List<Invoice>): List<Invoice> {
        val processedInvoices: MutableList<Invoice> = mutableListOf()
        invoices.forEach {
            processedInvoices.add(chargeInvoice(it))
        }

        return processedInvoices
    }

    private fun hasPendingInvoices(invoices: List<Invoice>): Boolean {
        return invoices.any { it.status == InvoiceStatus.PENDING }
    }

    private fun setPendingInvoicesToFailed(invoices: List<Invoice>) {
        invoices.forEach {
            invoiceService.updateInvoice(it.id, InvoiceStatus.FAILED.toString())
        }
    }
}
