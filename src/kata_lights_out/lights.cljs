(ns kata-lights-out.lights
  (:require  [com.stuartsierra.component :as component]))

(def ^:private light-off 0)

(defprotocol LightsGateway
  (reset-lights! [this m n])
  (flip-light! [this pos]))

(defn light-off? [light]
  (= light light-off))

(defn all-lights-off? [lights]
  (every? light-off? (flatten lights)))
