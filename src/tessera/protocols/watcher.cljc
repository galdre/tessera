(ns tessera.protocols.watcher)

(defprotocol Watcher
  (notify [watcher state-change] "Notify the watcher of a state
  change. If this induces a state change in another tessera, returns
  another state-change. Otherwise, returns nil."))
