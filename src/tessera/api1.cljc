(ns tessera.api1)

(comment
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
    :submit-on             #{:construct :ready :yield}
    :compute-times         #{int? :forever :always}})

  (t/compute
   {:compute-fn            (fn [deps])
    :compute-on            #{:creation :yield}
    :idempotent-submission #{true false}
    :compute-times         #{int? :forever :always}})

  (t/static
   {:static x})

  (t/dynamic
   {:init x ; optional
    :deliver-validator (fn [token])
    :deliver-times #{int? :forever}})

  (t/none)

  )
