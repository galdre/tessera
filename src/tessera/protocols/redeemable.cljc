(ns tessera.protocols.redeemable)

(defprotocol Redeemable
  (can-redeem? [tessera] "Can the tessera be redeemed right now without blocking?")
  (redeem [tessera] "Returns the value for which this tessera can be
  redeemed. Throws an error if the tessera cannot be redeemed.")
  #?(:clj
     (redeem! [tessera] "Returns the value for which this tessera can be
  redeemed. Blocks if necessary. Throws an error if the tessera is in
  a failed state.")))
