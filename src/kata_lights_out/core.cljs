(ns kata-lights-out.core
  (:require
    [kata-lights-out.lights-view :as lights-view]
    [cljs.core.async :as async]
    [com.stuartsierra.component :as component]
    [kata-lights-out.lights :as lights]
    [kata-lights-out.local-lights :as local-lights]
    [kata-lights-out.remote-lights :as remote-lights]))

(enable-console-print!)

;; -------------------------
;; Initialize app
(defrecord MainComponent [lights-component m n]
  component/Lifecycle
  (start [this]
    (println ";; Starting main component")
    (lights/reset-lights! lights-component m n)
    (lights-view/mount lights-component)
    this)

  (stop [this]
    (println ";; Stopping lights component")
    this))

(defn main-component [m n]
  (map->MainComponent {:n n :m m}))

(defn system-using-remote-lights
  ""
  [m n]
  (component/system-map
   :lights-component (remote-lights/make nil (async/chan))
   :main (component/using
          (main-component m n)
          [:lights-component])))

(defn system-using-local-lights
  ""
  [m n]
  (component/system-map
   :lights-component (local-lights/make)
   :main (component/using
          (main-component m n)
          [:lights-component])))


(defn init! [m n make-lights-component]
  (component/start
   (make-lights-component m n)))

(init! 3 3 system-using-local-lights)
