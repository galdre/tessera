(ns tessera.impl.promise
  (:require [tessera.protocols.deliverable :as deliver]
            [tessera.protocols.redeemable :as redeem]
            [tessera.protocols.revokable :as revoke]
            [tessera.protocols.state-change :as change]
            [tessera.protocols.status :as status]
            [tessera.protocols.tessera :as tessera]
            [tessera.protocols.watcher :as watch]))

(deftype Promise
    [^:mutable fulfilled
     ^:mutable failure
     ^:mutable value
     ^:mutable revoked
     ^:mutable watchers]
  tessera/Tessera
  (status [_]
    (cond revoked (revoked-status)
          (not fulfilled) (pending-status)
          (some? failure) (failed-status)
          :else (success-status)))
  (add-watcher [this watcher] (tessera/add-watcher this (uuid) watcher))
  (add-watcher [this token watcher]
    (set! watchers (assoc watchers token watcher))
    ;; Maybe don't:
    (->> (change/->simple-state-change this (tessera/status this) value)
         (watch/notify watcher)))
  (remove-watcher [_ token]
    (set! watchers (dissoc watchers token))
    nil) ; TODO: decide return contract
  (dependencies [_] nil) 
  redeem/Redeemable
  (can-redeem? [_] (some? fulfilled))
  (redeem [_] (or failure value))
  revoke/Revokable
  (can-revoke? [_] (nil? fulfilled))
  (revoke [this] (revoke/revoke this nil))
  (revoke [_ _] (set! revoked true) nil) ; TODO: return state-change
  deliver/Deliverable
  (can-deliver? [_] (and (not revoked) (not fulfilled)))
  (deliver [this value] (deliver/deliver this nil value))
  (deliver [this _ v]
    (set! fulfilled true)
    (set! value v)
    (doseq [watcher watchers]
      (->> (change/->simple-state-change this (tessera/status this) value)
           (watch/notify watcher))))
  (fumble [this error] (deliver/fumble this nil error))
  (fumble [this _ error]
    (set! fulfilled true)
    (set! failure error)
    (doseq [watcher watchers]
      (->> (change/->simple-state-change this (tessera/status this) failure)
           (watch/notify watcher)))))
