(ns tessera.impl.delay
  (:require [tessera.protocols.redeemable :as redeem]
            [tessera.protocols.state-change :as change]
            [tessera.protocols.status :as status]
            [tessera.protocols.tessera :as tessera]
            [tessera.protocols.watcher :as watch])
  (:refer-clojure :exclude [->Delay Delay]))

(defn ready-status
  []
  (status/->simple-status
   {::status/ready true
    ::status/pending true}))

(defn failure-status
  [failure]
  (status/->simple-status
   {::status/ready true
    ::status/failed true
    ::failure failure}))

(defn success-status
  []
  (status/->simple-status
   {::status/ready true
    ::status/succeeded true}))

(deftype Delay
    [thunk
     ^:mutable value
     ^:mutable failure
     ^:mutable fulfilled
     ^:mutable watchers]
  tessera/Tessera
  (status [_]
    (cond (not fulfilled) (ready-status)
          (some? failure) (failure-status failure)
          :else (success-status)))
  (add-watcher [this watcher] (tessera/add-watcher this (random-uuid) watcher))
  (add-watcher [this token watcher]
    (set! watchers (assoc watchers token watcher)))
  (remove-watcher [_ token]
    (set! watchers (dissoc watchers token))
    nil) ; TODO: decide return contract
  (watchers [_] (vals watchers))
  (dependencies [_] nil)
  redeem/Redeemable
  (can-redeem? [_] true)
  (redeem [this]
    (if fulfilled
      (or failure value)
      (let [v (try (thunk)
                   (catch js/Error e
                     (set! failure e)
                     ::failed))]
        (when (not= v ::failed)
          (set! value v))
        (set! fulfilled true)
        (change/->simple-state-change this
                                      (tessera/status this)
                                      (or failure value))))))

(deftype ReactiveDelay
    [thunk
     dependencies
     dependency->watch-token
     ^:mutable dependency->value
     ^:mutable value
     ^:mutable failure
     ^:mutable fulfilled
     ^:mutable watchers]
  tessera/Tessera
  (status [_]
    (cond (not fulfilled) (ready-status)
          (some? failure) (failure-status failure)
          :else (success-status)))
  (add-watcher [this watcher]
    (tessera/add-watcher this (random-uuid) watcher))
  (add-watcher [this token watcher]
    (set! watchers (assoc watchers token watcher)))
  (remove-watcher [this token]
    (set! watchers (dissoc watchers token))
    nil) ; TODO: decide return contract
  (watchers [this] (vals watchers))
  (dependencies [this] dependencies)
  redeem/Redeemable
  (can-redeem? [_]
    (every? redeem/can-redeem? dependencies))
  (redeem [this]
    (if fulfilled
      (or failure value)
      (let [inputs (map dependency->value dependencies)
            v (try (apply thunk inputs)
                   (catch js/Error e
                     (set! failure e)
                     ::failed))]
        (when (not= v ::failed)
          (set! value v))
        (set! fulfilled true)
        (change/->simple-state-change this
                                      (tessera/status this)
                                      (or failure value)))))
  watch/Watcher
  (notify [this state-change]
    (set! fulfilled false)
    (set! dependency->value
          (assoc dependency->value
                 (change/tessera state-change)
                 (change/value state-change)))
    (change/->simple-state-change this
                                  (tessera/status this)
                                  (or failure value))))
