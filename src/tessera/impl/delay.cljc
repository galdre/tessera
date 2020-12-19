(ns tessera.impl.delay
  (:require [tessera.protocols.redeemable :as redeem]
            [tessera.protocols.tessera :as tessera]
            [tessera.protocols.watcher :as watcher]))

(deftype Delay
    [thunk
     ^:mutable value
     ^:mutable failure
     ^:mutable fulfilled
     ^:mutable watchers]
  tessera/Tessera
  (status [_]
    (if (some? failure) ; TODO: use actual statuses, incorporate new one for delay
      ::failed
      ::succeeded))
  (add-watcher [this watcher] (tessera/add-watcher this (uuid) watcher))
  (add-watcher [_ token watcher]
    (set! watchers (assoc watchers token watcher))
    (notify watcher :state-change)) ; TODO
  (remove-watcher [_ token]
    (set! watchers (dissoc watchers token)))
  (watchers [_] (vals watchers))
  (dependencies [_] nil)
  redeem/Redeemable
  (can-redeem? [_] true)
  (redeem [_]
    (if fulfilled
      value
      (let [v (try (thunk)
                   (catch js/Error e
                     (set! failure e)
                     ::failed))]
        (when (not= v ::failed)
          (set! value v))
        (set! fulfilled true)))))
