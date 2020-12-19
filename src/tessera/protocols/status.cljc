(ns tessera.protocols.status)

(defprotocol Status
  (blocked? [status] "Is the tessera blocked by a dependency?")
  (pending? [status] "Is the tessera currently being fulfilled?")
  (revoked? [status] "Has the tessera been revoked?")
  (ready? [status] "Is the tessera ready to be redeemed?")
  (failed? [status] "Is the tessera in a state of failure?")
  (succeeded? [status] "Is the tessera in a state of success?")
  (details [status] "Returns a map of data concerning the status, with
   special keys {::keys [blocked pending revoked ready failed
   succeeded]}."))

(defn ->simple-status
  [{::keys [blocked pending revoked ready failed succeeded] :as details}]
  (reify Status
    (blocked? [_] (boolean blocked))
    (pending? [_] (boolean pending))
    (revoked? [_] (boolean revoked))
    (ready? [_] (boolean ready))
    (failed? [_] (boolean failed))
    (succeeded? [_] (boolean succeeded))
    (details [_] details)))
