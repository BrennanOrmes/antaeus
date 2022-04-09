package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.random.Random.Default.nextInt

class BillingServiceTest {

    private val invoice = createInvoice()
    private val invoiceNotFound = createInvoice()
    private val invoiceNotEnoughMoney = createInvoice()
    private val invoiceCustomerNotFound = createInvoice()
    private val invoiceCurrencyMismatch = createInvoice()
    private val invoiceNetworkError = createInvoice()

    private val invoiceService = mockk<InvoiceService> {
        every { updateInvoice(invoice.id, InvoiceStatus.PAID.toString()) } returns invoice
        every { updateInvoice(invoice.id, InvoiceStatus.FAILED.toString()) } returns invoice
        every { updateInvoice(
            invoiceNotEnoughMoney.id, InvoiceStatus.FAILED.toString())
        } returns createInvoice(invoiceNotEnoughMoney.id, invoiceNotEnoughMoney.customerId, InvoiceStatus.FAILED)
        every { updateInvoice(
            invoiceCustomerNotFound.id, InvoiceStatus.FAILED.toString())
        } returns createInvoice(invoiceCustomerNotFound.id, invoiceCustomerNotFound.customerId, InvoiceStatus.FAILED)
        every { updateInvoice(
            invoiceCurrencyMismatch.id, InvoiceStatus.FAILED.toString())
        } returns createInvoice(invoiceCurrencyMismatch.id, invoiceCurrencyMismatch.customerId, InvoiceStatus.FAILED)
        every { updateInvoice(
            invoiceNetworkError.id, InvoiceStatus.PENDING.toString())
        } returns createInvoice(invoiceNetworkError.id, invoiceNetworkError.customerId, InvoiceStatus.PENDING)
        every { fetch(invoice.id) } returns invoice
        every { fetch(invoiceCustomerNotFound.id) } returns invoiceCustomerNotFound
        every { updateInvoice(invoiceNotFound.id, any()) }.throws(InvoiceNotFoundException(invoiceNotFound.id))
    }

    private val paymentProvider = mockk<PaymentProvider> {
        every { charge(any()) } returns true
        every { charge(invoiceNotEnoughMoney) } returns false
        every { charge(invoiceCustomerNotFound) }.throws(CustomerNotFoundException(invoiceCustomerNotFound.customerId))
        every { charge(invoiceCurrencyMismatch) }.throws(CurrencyMismatchException(
            invoiceCurrencyMismatch.id, invoiceCurrencyMismatch.customerId)
        )
        every { charge(invoiceNetworkError) }.throws(NetworkException())
    }

    private fun createInvoice(
        id: Int = nextInt(), customerId: Int = nextInt(), status: InvoiceStatus = InvoiceStatus.PENDING
    ): Invoice {
        return Invoice(
            id = id,
            customerId = customerId,
            amount = Money(BigDecimal.valueOf(100), Currency.DKK),
            status = status
        )
    }

    private val billingService = BillingService(paymentProvider = paymentProvider, invoiceService = invoiceService)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            billingService.chargeInvoice(invoiceNotFound)
        }
    }

    @Test
    fun `will set invoice status to FAILED if currency mismatch`() {
        val updatedInvoice = billingService.chargeInvoice(invoiceCurrencyMismatch)
        assert(updatedInvoice.status == InvoiceStatus.FAILED)
    }

    @Test
    fun `will set invoice status to FAILED if customer not found`() {
        val updatedInvoice = billingService.chargeInvoice(invoiceCustomerNotFound)
        assert(updatedInvoice.status == InvoiceStatus.FAILED)
    }

    @Test
    fun `will set invoice status to PENDING if network error`() {
        val updatedInvoice = billingService.chargeInvoice(invoiceNetworkError)
        assert(updatedInvoice.status == InvoiceStatus.PENDING)
    }
}
