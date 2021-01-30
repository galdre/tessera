(ns tessera.core
  (:require
   [tessera.impl.watcher-fn :as w-fn]
   [tessera.protocols.deliverable :as deliver]
   [tessera.protocols.redeemable :as redeem]
   [tessera.protocols.revokable :as revoke]
   [tessera.protocols.state-change :as change]
   [tessera.protocols.status :as status]
   [tessera.protocols.tessera :as tessera]
   [tessera.protocols.watcher :as watch]
   #?@(:cljs
       [[tessera.impl.delay :as delay]
        [tessera.impl.promise :as promise]])))

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
    ::error #_(error/irredeemable!)
    (let [redemption (redeem/redeem tessera)]
      (if (change/state-change? redemption)
        (do (notify-watchers redemption)
            (change/value redemption))
        redemption))))

#?(:clj
   (defn redeem!
     [tessera]
     (redeem/redeem! tessera)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic functions for all revokable tesserae

(defn- tokenize
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
     (when-let [state-change ((tokenize revoke/revoke token) tessera)]
       (notify-watchers state-change)
       true))))

(defn revoke-all
  ([tessera] (revoke-all tessera nil))
  ([tessera token]
   (letfn [(trigger-recursively [state-change token]
             (let [revoke-all* (tokenize revoke-all token)]
               (doseq [dependency (-> state-change change/tessera tessera/dependencies)]
                 (revoke-all* dependency))))]
     (when (revoke/can-revoke? tessera)
       (when-let [state-change ((tokenize revoke/revoke token) tessera)]
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
     (when-let [state-change ((tokenize deliver/deliver token) tessera value)]
       (notify-watchers state-change))
     true)))

(defn fumble
  ([tessera error] (fumble tessera nil error))
  ([tessera token error]
   (when (deliver/can-deliver? tessera)
     (when-let [state-change ((tokenize deliver/fumble token) tessera error)]
       (notify-watchers state-change)
       true))))

;; cljs primitives

#?(:cljs
   (defn delay* [f] (delay/->Delay f nil nil false {})))

#?(:cljs
   (defn promise* [] (promise/->Promise false nil nil false {})))

#?(:cljs
   (defn watcher* [f] (w-fn/->watcher-fn f)))

#?(:cljs
   (defn watch* [tessera watcher-fn]
     (->> (watcher* watcher-fn)
          (tessera/add-watcher tessera))))
