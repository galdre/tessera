(ns tessera.impl.delay
  (:require [tessera.protocols.tessera :as tessera]
            [tessera.protocols.redeemable :as redeem]))

(deftype Delay
    [f
     ^:mutable value
     ^:mutable failure
     ^:mutable fulfilled])
