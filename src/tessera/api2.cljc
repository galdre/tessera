(ns tessera.api2)

(comment
  ;; submit on the previous executor
  (t/tessera
   {:dependencies {:prev previous-tessera}
    :value        (t/executor
                   {:init          {}
                    :submit-fn     (fn [job]
                                     (let [es (t/executor prev)]
                                       (t/submit es job)))
                    :submit-on     t/READY
                    :compute-fn    (fn [] (process prev))
                    :compute-times 1})})
  ;; submit on the current executor
  (t/tessera
   {:dependencies {:prev previous-tessera}
    :value        (t/executor
                   {:init          {}
                    :submit-fn     (let [es app/*context-executor*]
                                     (fn [job]
                                       (t/submit es job)))
                    :submit-on     t/READY
                    :compute-fn    (fn [] (process prev))
                    :compute-times 1})})

  ;; timeout race:
  (t/tessera
   {:dependencies {:command async-command
                   :timeout timeout}
    :ready-when   (fn []
                    (or (t/ready? &async-command)
                        (t/ready? &timeout)))
    :value        (t/function
                   {:compute-fn    (fn []
                                     (cond
                                       (t/ready? &async-command)
                                       async-command
                                       (t/ready? &timeout)
                                       ::timeout))
                    :compute-on    t/READY
                    :compute-times 1})})

  ;; Promise (with single delivery):
  (t/tessera
   {:value (t/deliver
            {:init          nil
             :deliver-times 1
             :deliver-fn    identity})})

  ;; Atom:
  (t/tessera
   {:value (t/deliver
            {:init {}
             :compute-fn (fn [f & args]
                           (apply f &value args))})})

  ;; Still not properly factoring apart concerns.
  )
