package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {

    private val pendingInvoice = createInvoice(InvoiceStatus.PENDING)

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { fetchInvoices() } returns listOf(pendingInvoice)
    }

    private fun createInvoice(status: InvoiceStatus): Invoice {
        return Invoice(
            id = 1,
            customerId = 2,
            amount = Money(BigDecimal.valueOf(100), Currency.GBP),
            status = status
        )
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `get invoices by PENDING status`() {
        val invoices = invoiceService.fetchAllByStatus(InvoiceStatus.PENDING.toString())
        assert(invoices.all { it.status == InvoiceStatus.PENDING })
    }

    @Test
    fun `will throw if wrong status is given`() {
        assertThrows<IllegalArgumentException> {
            invoiceService.fetchAllByStatus("blah")
        }
    }

    @Test
    fun `set invoice status to PAID`() {
        every { dal.updateInvoiceStatus(1, InvoiceStatus.PAID) } returns createInvoice(InvoiceStatus.PAID)
        val updatedInvoice = invoiceService.updateInvoice(pendingInvoice.id, InvoiceStatus.PAID.toString())
        assert(updatedInvoice.status != pendingInvoice.status)
    }
}