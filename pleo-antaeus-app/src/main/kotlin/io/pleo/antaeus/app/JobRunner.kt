package io.pleo.antaeus.app

import io.pleo.antaeus.core.services.BillingService
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.scheduling.cron.Cron

class JobRunner(
    private val jobScheduler: JobScheduler
) {
    fun enqueueBillingBatchJob() {
        jobScheduler.scheduleRecurrently<BillingService>(Cron.monthly(1)) { x: BillingService -> x.batch() }
    }
}