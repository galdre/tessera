(ns tessera.protocols.state-change)

(defprotocol StateChange
  (tessera [state-change] "Which tessera underwent the state change?")
  (status [state-change] "What is the new status of the tessera?")
  (value [state-change] "What is the new value, if applicable, of the tessera?"))

(defn ->simple-state-change
  [tessera status value]
  (reify StateChange
    (tessera [_] tessera)
    (status [_] status)
    (value [_] value)))
