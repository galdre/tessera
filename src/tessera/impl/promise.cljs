(ns tessera.impl.promise
  (:require [tessera.protocols.deliverable :as deliver]
            [tessera.protocols.redeemable :as redeem]
            [tessera.protocols.revokable :as revoke]
            [tessera.protocols.state-change :as change]
            [tessera.protocols.status :as status]
            [tessera.protocols.tessera :as tessera]
            [tessera.protocols.watcher :as watch]))

(defn revoked-status
  []
  (status/->simple-status
   {::status/revoked true}))

(defn pending-status
  []
  (status/->simple-status
   {::status/pending true}))

(defn failed-status
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
          (some? failure) (failed-status failure)
          :else (success-status)))
  (add-watcher [this watcher] (tessera/add-watcher this (random-uuid) watcher))
  (add-watcher [this token watcher]
    (set! watchers (assoc watchers token watcher)))
  (remove-watcher [_ token]
    (set! watchers (dissoc watchers token))
    nil) ; TODO: decide return contract
  (dependencies [_] nil)
  (watchers [_] (vals watchers))
  redeem/Redeemable
  (can-redeem? [_] (true? fulfilled))
  (redeem [_] (or failure value))
  revoke/Revokable
  (can-revoke? [_] (nil? fulfilled))
  (revoke [this] (revoke/revoke this nil))
  (revoke [_ _] (set! revoked true) nil) ; TODO: return state-change
  deliver/Deliverable
  (can-deliver? [_] (and (not revoked) (not fulfilled)))
  (deliver [this value] (deliver/deliver this nil value))
  (deliver [this _ v]
    (when (deliver/can-deliver? this)
      (set! fulfilled true)
      (set! value v)
      (change/->simple-state-change this (tessera/status this) value)))
  (fumble [this error] (deliver/fumble this nil error))
  (fumble [this _ error]
    (when (deliver/can-deliver? this)
      (set! fulfilled true)
      (set! failure error)
      (change/->simple-state-change this (tessera/status this) failure))))
