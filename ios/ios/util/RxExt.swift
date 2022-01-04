//
//  RxExt.swift
//  todo-mvi-rxswift-swift
//
//  Created by damien on 03/12/2021.
//

import Foundation
import RxSwift
import shared

extension ObservableType {
    func compose<T>(_ transform: (RxSwift.Observable<Self.Element>) -> RxSwift.Observable<T>) -> RxSwift.Observable<T> {
        return transform(self.asObservable())
    }
}

typealias ObservableTransformer<I, O> = (RxSwift.Observable<I>) -> RxSwift.Observable<O>

extension RxSwift.Observable where Element : AnyObject {
    static func from(_ observable: ObservableWrapper<Element>) -> RxSwift.Observable<Element> {
        return RxSwift.Observable<Element>.create { observer in
            let disposable = observable.subscribe(
                isThreadLocal: true,
                onError: { observer.onError(KotlinError($0)) },
                onComplete: observer.onCompleted,
                onNext: observer.onNext
            )

            return Disposables.create(with: disposable.dispose)
        }
    }
}

extension RxSwift.Single where Element : AnyObject {
    static func from(_ single: SingleWrapper<Element>) -> RxSwift.Single<Element> {
        return RxSwift.Single<Element>.create { observer in
            let disposable = single.subscribe(
                isThreadLocal: true,
                onError: { observer(.failure(KotlinError($0))) },
                onSuccess: { observer(.success($0)) }
            )

            return Disposables.create(with: disposable.dispose)
        }
    }
}

extension RxSwift.Maybe where Element : AnyObject {
    static func from(_ maybe: MaybeWrapper<Element>) -> RxSwift.Maybe<Element> {
        return RxSwift.Maybe<Element>.create { observer in
            let disposable = maybe.subscribe(
                isThreadLocal: true,
                onError: { observer(.error(KotlinError($0))) },
                onComplete: { observer(.completed) },
                onSuccess: { observer(.success($0)) }
            )

            return Disposables.create(with: disposable.dispose)
        }
    }
}

extension RxSwift.Completable {
    static func from(_ completable: CompletableWrapper) -> RxSwift.Completable {
        return RxSwift.Completable.create { observer in
            let disposable = completable.subscribe(
                isThreadLocal: true,
                onError: { observer(.error(KotlinError($0))) },
                onComplete: { observer(.completed) }
            )

            return Disposables.create(with: disposable.dispose)
        }
    }
}

struct KotlinError : Error {
    let throwable: KotlinThrowable

    init (_ throwable: KotlinThrowable) {
        self.throwable = throwable
    }
}

