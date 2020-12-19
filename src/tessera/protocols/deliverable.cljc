(ns tessera.protocols.deliverable)

(defprotocol Deliverable
  (can-deliver? [tessera] "Can we deliver a value or an error to this tessera now?")
  (deliver [tessera value] [tessera token value] "Deliver `value` to
  this tessera. May take a token for validation.")
  (fumble [tessera error] [tessera token value] "Deliver `error` to
  this tessera. May take a token for validation."))
