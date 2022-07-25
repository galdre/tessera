(ns tessera.api1
  "Goal: be able to describe (most) common models.
  NOT necessary to describe absolutely all; protocols can be
  implemented directly.

  First, make it work. Then think about optimizations.")

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
   {:value            #{:executor :function :static :dynamic :none}
    :dependencies     {:dep-1 dep-1
                       :dep-2 dep-2}
    ;; Optional:
    :revoke-validator (fn [token])
    :yield-validator  (fn [token])
    :buffer           :BUFFER-SPEC})

  (t/executor
   {:submit-fn             (submit-fn [deps])
    :compute-fn            (fn [deps])
    :idempotent-submission #{true false} ; collapse to one computation
    :submit-on             #{t/CONSTRUCTED t/READY t/UNREADY t/STALE t/YIELDING t/DELIVERING}
    :compute-times         #{(t/times int?) t/UNREADY t/YIELDING}})

  (t/function
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

  (t/none)

  (t/buffer
   {:size #{int? :infinite}
    :queue #{:fifo :lifo :priority}
    ;; vv drop, throw, block?
    :overflow-fn (fn [?])}))
