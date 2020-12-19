(ns tessera.protocols.tessera)

(defprotocol Tessera
  (status [tessera] "Returns a `Status`.")
  (add-watcher [tessera watcher] [tessera token watcher] "Registers
  the given watcher with this tessera, returning a token that can be
  used to remove the Watcher.")
  (remove-watcher [tessera token] "Unregisters any Watcher registered
  via this token.")
  (watchers [tessera] "Returns a sequence of Watchers registered with
  this tessera.")
  (dependencies [tessera] "Returns a sequence of tesserae upon which
  this tessera is dependent."))
