package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BillingServiceTest {

    private val invoiceNotFound = createInvoice(404, 1, InvoiceStatus.PENDING)
    private val invoiceCustomerNotFound = createInvoice(11, 404, InvoiceStatus.PENDING)
    private val invoiceCurrencyMismatch = createInvoice(1, 1, InvoiceStatus.PENDING)

    private val billingService = mockk<BillingService> {
        every { chargeInvoice(any()) } returns createInvoice(1, 1, InvoiceStatus.PAID)
        every { chargeInvoice(invoiceNotFound) }.throws(InvoiceNotFoundException(invoiceNotFound.id))
        every { chargeInvoice(invoiceCustomerNotFound) }.throws(CustomerNotFoundException(invoiceCustomerNotFound.id))
        every { chargeInvoice(invoiceCurrencyMismatch) }.throws(CurrencyMismatchException(
            invoiceCurrencyMismatch.id, invoiceCurrencyMismatch.customerId)
        )
    }

    private fun createInvoice(id: Int, customerId: Int, status: InvoiceStatus): Invoice {
        return Invoice(
            id = id,
            customerId = customerId,
            amount = Money(BigDecimal.valueOf(100), Currency.DKK),
            status = status
        )
    }

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            billingService.chargeInvoice(invoiceNotFound)
        }
    }

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<CustomerNotFoundException> {
            billingService.chargeInvoice(invoiceCustomerNotFound)
        }
    }

    @Test
    fun `will throw if currency is mismatched`() {
        assertThrows<CurrencyMismatchException> {
            billingService.chargeInvoice(invoiceCurrencyMismatch)
        }
    }
}
