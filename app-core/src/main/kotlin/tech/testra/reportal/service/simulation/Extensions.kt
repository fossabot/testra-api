package tech.testra.reportal.service.simulation

import tech.testra.reportal.domain.valueobjects.Percentile
import tech.testra.reportal.domain.valueobjects.SimulationScenario
import tech.testra.reportal.model.Percentile as PercentileInModel
import tech.testra.reportal.model.SimulationScenario as SimulationScenarioInModel

fun List<SimulationScenarioInModel>.toEntity(): List<SimulationScenario> =
    this.map {
        SimulationScenario(
            request = it.request,
            startTime = it.startTime,
            endTime = it.endTime,
            durationInMs = it.durationInMs,
            count = it.count,
            successCount = it.successCount,
            errorCount = it.errorCount,
            min = it.min,
            max = it.max,
            percentiles = it.percentiles.toDomainVO(),
            average = it.average,
            stdDiv = it.stdDiv,
            avgRequestPerSec = it.avgRequestPerSec
        )
    }

fun List<PercentileInModel>.toDomainVO(): List<Percentile> = this.map { Percentile(n = it.n, value = it.value) }
