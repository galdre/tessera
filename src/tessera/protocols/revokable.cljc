(ns tessera.protocols.revokable)

(defprotocol Revokable
  (can-revoke? [tessera] "Can this tessera be revoked now?")
  (revoke [tessera] [tessera token] "Revoke this tessera. Optionally accepts a token."))
