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
     * @return returns updated invoice as [Invoice]
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
}
