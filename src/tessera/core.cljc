(ns tessera.core
  (:require [tessera.impl.watcher-fn :as w-fn]
            [tessera.protocols.deliverable :as deliver]
            [tessera.protocols.redeemable :as redeem]
            [tessera.protocols.revokable :as revoke]
            [tessera.protocols.state-change :as change]
            [tessera.protocols.status :as status]
            [tessera.protocols.tessera :as tessera]
            [tessera.protocols.watcher :as watch]))

;; TODO: write macro to enable compile-time optimizations
(def ^:dynamic *unsafe* false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic functions for all tesserae

(defn tessera?
  [x]
  (satisfies? tessera/Tessera x))

(defn pending?
  [tessera]
  (-> tessera tessera/status status/pending?))

(defn failed?
  [tessera]
  (-> tessera tessera/status status/failed?))

(defn succeeded?
  [tessera]
  (-> tessera tessera/status status/succeeded?))

(defn watch
  [tessera f]
  (->> (w-fn/->WatcherFn f)
       (tessera/add-watcher tessera)))

(defn unwatch
  [tessera token]
  (tessera/remove-watcher tessera token))

(defn- notify-watchers
  [state-change]
  (loop [[change & changes] [state-change]]
    (when change
      (recur
       (into changes
             (keep #(watch/notify % change))
             (-> change change/tessera tessera/watchers))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic functions for all redeemable tesserae

(defn redeemable?
  [x]
  (and (tessera? x)
       (satisfies? redeem/Redeemable x)))

(defn ready?
  [tessera]
  (-> tessera tessera/status status/ready?)
  #_(and (or *unsafe* (satisfies? redeem/Redeemable x))
       (redeem/can-redeem? tessera)))

(defn redeem
  [tessera]
  (if-not (redeem/can-redeem? tessera)
    (error/irredeemable!)
    (redeem/redeem tessera)))

#?(:clj
   (defn redeem!
     [tessera]
     (redeem/redeem! tessera)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic functions for all revokable tesserae

(defn- maybe-token
  [f token]
  (if token
    (fn [tessera & args] (apply f tessera token args))
    (fn [tessera & args] (apply f tessera args))))

(defn revokable?
  [x]
  (and (tessera? x)
       (satisfies? revoke/Revokable x)))

(defn revoked?
  [tessera]
  (-> tessera tessera/status status/revoked?))

(defn revoke
  ([tessera] (revoke tessera nil))
  ([tessera token]
   (when (revoke/can-revoke? tessera)
     (when-let [state-change ((maybe-token revoke/revoke token) tessera)]
       (notify-watchers state-change)
       true))))

(defn revoke-all
  ([tessera] (revoke-all tessera nil))
  ([tessera token]
   (letfn [(trigger-recursively [state-change token]
             (let [revoke-all* (maybe-token revoke-all)]
               (doseq [dependency (-> state-change change/tessera tessera/dependencies)]
                 (revoke-all* dependency))))]
     (when (revoke/can-revoke? tessera)
       (when-let [state-change (maybe-token revoke/revoke tessera token)]
         (notify-watchers state-change)
         (trigger-recursively state-change token)
         true)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic functions for all deliverable tesserae

(defn deliverable?
  [x]
  (and (tessera? x)
       (satisfies? deliver/Deliverable x)))

(defn deliver
  ([tessera value] (deliver tessera nil value))
  ([tessera token value]
   (when (deliver/can-deliver? tessera)
     (when-let [state-change ((maybe-token deliver/deliver token) this value)]
       (notify-watchers state-change)
       true))))

(defn fumble
  ([tessera error] (fumble tessera nil error))
  ([tessera token error]
   (when (deliver/can-deliver? tessera)
     (when-let [state-change ((maybe-token deliver/fumble token) tessera error)]
       (notify-watchers state-change)
       true))))
