(ns tessera.protocols.status)

(defprotocol Status
  (blocked? [status] "Is the tessera blocked by a dependency?")
  (pending? [status] "Is the tessera currently being fulfilled?")
  (revoked? [status] "Has the tessera been revoked?")
  (failed? [status] "Is the tessera in a state of failure?")
  (succeeded? [status] "Is the tessera in a state of success?")
  (details [status] "Returns a map of data concerning the status, including
   {::keys [blocked pending revoked failed succeeded]}."))
