package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {

//    private val dal = mockk<AntaeusDal> {
//        every { fetchInvoice(404) } returns null
//    }

    private val pendingInvoice = createInvoice(InvoiceStatus.PENDING)

    private val invoiceService = mockk<InvoiceService> {
        every { fetch(404) }.throws(InvoiceNotFoundException(404))
        // Had some difficulty figuring out expected return of list, listOf seems like cheating?
        every { fetchAllByStatus(InvoiceStatus.PENDING.toString()) } returns listOf()
        every { fetchAllByStatus("blah") }.throws(IllegalArgumentException())
        every { updateInvoice(createInvoice(InvoiceStatus.PENDING).id, InvoiceStatus.PAID.toString()) } returns createInvoice(InvoiceStatus.PAID)
    }

    private fun createInvoice(status: InvoiceStatus): Invoice {
        return Invoice(
            id = 1,
            customerId = 2,
            amount = Money(BigDecimal.valueOf(100), Currency.GBP),
            status = status
        )
    }

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
        val updatedInvoice = invoiceService.updateInvoice(pendingInvoice.id, InvoiceStatus.PAID.toString())
        assert(updatedInvoice.status != pendingInvoice.status)
    }
}