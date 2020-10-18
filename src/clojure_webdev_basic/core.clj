(ns clojure-webdev-basic.core
  (:require [clojure-webdev-basic.item.model :as items]
            [clojure-webdev-basic.item.handler :refer [handle-index-items
                                                       handle-create-item
                                                       handle-delete-item
                                                       handle-update-item]])
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [compojure.core :refer [defroutes ANY GET POST PUT DELETE]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]))

(def db (or
         (System/getenv "DATABASE_URL")
         "jdbc:postgresql://localhost/clojure-webdev-basic"))

(defn greet [req]
  {:status 200
   :body "Hello world"
   :headers {}})
(defn goodbye [req]
  {:status 200
   :body "Goodbye page"
   :headers {}})
(defn about [req]
  {:status 200
   :body "About page"
   :headers {}})
(defn yo [req]
  (let [name (get-in req [:route-params :name])]
    {:status 200
     :body (str "Yo " name "!")
     :headers {}}))

(defroutes routes
  (GET "/" [] greet)
  (GET "/goodbye" [] goodbye)
  (GET "/about" [] about)
  (GET "/request" [] handle-dump)
  (GET "/yo/:name" [] yo)

  (GET "/items" [] handle-index-items)
  (POST "/items" [handle-create-item])
  (DELETE "/items/:item-id" [] handle-delete-item)
  (PUT "/items/:item-id" [] handle-update-item)

  (not-found "Page not found"))

(defn wrap-db [hdlr]
  (fn [req]
    (hdlr (assoc req :clojure-webdev-basic/db db))))
(defn wrap-server [hdlr]
  (fn [req]
    (hdlr (assoc-in (hdlr req) [:headers "Server"] "Listronica 9000"))))

(def sim-methods {"PUT" :put "DELETE" :delete})

(defn wrap-simulated-methods [hdlr]
  (fn [req]
    (if-let [method (and (= :post (:request-method req))
                         (sim-methods (get-in req [:params "_method"])))]
      (hdlr (assoc req :request-method method))
      (hdlr req))))


(def app
  (wrap-server
   (wrap-file-info
    (wrap-resource
     (wrap-db
      (wrap-params
       (wrap-simulated-methods routes)))
     "static"))))

(defn -main [port]
  (items/create-table db)
  (jetty/run-jetty app {:port (Integer. port)}))

(defn -dev-main [port]
  (items/create-table db)
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))