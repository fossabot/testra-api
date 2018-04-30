package tech.testra.reportal.extension

import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

fun <R, T> Mono<T>.flatMapWithResumeOnError(transformer: (T) -> Mono<R>): Mono<R> =
    this.flatMap { t -> transformer(t) }
        .onErrorResume { it.toMono() }

fun <R, T> Mono<T>.flatMapManyWithResumeOnError(transformer: (T) -> Flux<R>): Flux<R> =
    this.flatMapMany { t -> transformer(t) }
        .onErrorResume { it.toFlux() }

fun <R> Mono<R>.orElseGetException(ex: Exception): Mono<R> =
    this.switchIfEmpty(ex.toMono())
        .flatMap { this.toMono() }

fun <T> Mono<T>.onDuplicateKeyException(function: () -> Exception): Mono<T> {
    return this.onErrorResume(DuplicateKeyException::class.java) { function().toMono() }
}