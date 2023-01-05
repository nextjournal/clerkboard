(ns nextjournal.clerkboard
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [hiccup.core :as hiccup]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.analyzer :as analyzer]
            [nextjournal.clerk.eval :as eval]
            [nextjournal.clerk.parser :as parser]
            [nextjournal.clerk.view :as view]
            [nextjournal.clerk.config :as config]
            [org.httpkit.server :as httpkit]))

(defn eval-notebook
  "Evaluates the notebook identified by its `ns-sym` and processes its block
  so re-db can sync them."
  [ns-sym]
  (->> (str (analyzer/ns->path ns-sym) ".clj")
       (io/resource)
       (eval/eval-file)))

(defn render-notebook [ns-sym]
  (view/->html (-> {:doc (eval-notebook ns-sym)}
                   (update :doc view/doc->viewer)
                   (assoc :conn-ws? false)
                   (assoc :resource->url @config/!resource->url))))

#_(render-notebook 'nextjournal.clerkboard.demo)

(defn app [{:as req :keys [uri]}]
  (try
    ;; TODO: introduce routing thing, get `ns-sym` from uri
    (case (get (re-matches #"/([^/]*).*" uri) 1)
      "" {:status 200
          :body (render-notebook 'nextjournal.clerkboard.demo)})
    (catch Throwable e
      {:status  500
       :body    (with-out-str (pprint/pprint (Throwable->map e)))})))

(defonce !server (atom nil))

(defn halt! []
  (when-let [{:keys [port stop-fn]} @!server]
    (stop-fn)
    (println (str "Webserver running on " port ", stopped."))
    (reset! !server nil)))

#_(halt!)

(defn serve! [{:keys [port] :or {port 7777}}]
  (halt!)
  (try
    (reset! !server {:port port :stop-fn (httpkit/run-server #'app {:port port})})
    (println (str "Clerkboard webserver started on http://localhost:" port " ..."))
    (catch java.net.BindException _e
      (println "Port " port " not available, server not started!"))))

#_(serve! {:port 9988})

