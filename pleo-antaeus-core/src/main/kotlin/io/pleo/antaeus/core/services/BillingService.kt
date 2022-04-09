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
     * Charged invoice using [PaymentProvider]
     * @param invoice as [Invoice]
     * @return returns updated invoice as [Invoice]
     * @throws IllegalArgumentException
     * @throws CurrencyMismatchException
     * @throws NetworkException
     * @throws CustomerNotFoundException
     * @throws InvoiceNotFoundException
     */
    fun chargeInvoice(invoice: Invoice): Invoice {
        // throw not found exception if invoice does not exist.
        invoiceService.fetch(invoice.id)

        return try {
            if(paymentProvider.charge(invoice)) {
                invoiceService.updateInvoice(invoice.id, InvoiceStatus.PAID.toString())
            } else {
                invoiceService.updateInvoice(invoice.id, InvoiceStatus.FAILED.toString())
            }
        } catch (exception: Exception) {
            when(exception) {
                is CurrencyMismatchException, is CustomerNotFoundException -> {
                    invoiceService.updateInvoice(invoice.id, InvoiceStatus.FAILED.toString())
                } is NetworkException -> {
                    // if paymentProvider throws a network error, we will keep the invoice as pending
                    return invoice
                } else -> {
                    // catch any unhandled exceptions here
                    throw exception
                }
            }
        }
    }
}
