(ns tessera.api1)

;; Reify the event lifecycle, allow specifying handlers for each lifecycle.

(comment
  ;; Triggers: (?)
  [t/CONSTRUCTED
   t/READY
   t/UNREADY
   t/STALE
   t/FRESH
   t/YIELDING
   t/DELIVERING]
  
  (t/tessera
   {:value            #{:compute :static :dynamic :none}
    :dependencies     {:dep-1 dep-1
                       :dep-2 dep-2}
    ;; Optional:
    :revoke-validator (fn [token])
    :yield-validator  (fn [token])
    :buffer           :BUFFER-SPEC})

  (t/executor
   {:submit-fn             (submit-fn [deps])
    :compute-fn            (fn [deps])
    :idempotent-submission #{true false}
    :submit-on             #{t/CONSTRUCTED t/READY t/UNREADY t/STALE t/YIELDING t/DELIVERING}
    :compute-times         #{(t/times int?) t/UNREADY t/YIELDING}})

  (t/compute
   {:compute-fn            (fn [deps])
    :compute-on            #{t/CONSTRUCTED t/YIELDING}
    :idempotent-submission #{true false}
    :compute-times         #{int? :forever :always}})

  (t/static
   {:static x})

  (t/dynamic
   {:init x ; optional
    :deliver-validator (fn [token])
    :deliver-times #{int? :forever}})

  (t/none))
