(ns tessera.impl.watcher-fn
  (:require [tessera.protocols.state-change :as change]
            [tessera.protocols.watcher :as watch]))

(deftype WatcherFn [f]
  watch/Watcher
  (notify [_ state-change]
    (f (change/status state-change) (change/value state-change))
    nil))

(defn ->watcher-fn
  [f]
  (->WatcherFn f))
