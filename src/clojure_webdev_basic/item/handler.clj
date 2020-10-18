(ns clojure-webdev-basic.item.handler
  (:require [clojure-webdev-basic.item.model :refer [create-item
                                                     read-items
                                                     update-item
                                                     delete-item]]
            [clojure-webdev-basic.item.view :refer [items-page]]))

(defn handle-index-items [req]
  (let [db (:clojure-webdev-basic/db req)
        items (read-items db)]
    {:status 200
     :headers {}
     :body items-page}))

(defn handle-create-item [req]
  (let [name (get-in req [:params "name"])
        description (get-in req [:params "description"])
        db (:clojure-webdev-basic/db req)
        item-id (create-item db name description)]
    {:status 302
     :headers {"Location" "/items"}
     :body ""}))

(defn handle-delete-item [req]
  (let [db (:clojure-webdev-basic/db req)
        item-id (java.util.UUID/fromString (:item-id (:route-params req)))
        exists? (delete-item db item-id)]
    (if exists?
      {:status 302
       :headers {"Location" "/items"}
       :body ""}
      {:status 404
       :body "List not found"
       :headers {}})))

(defn handle-update-item [req]
  (let [db (:clojure-webdev-basic/db req)
        item-id (java.util.UUID/fromString (:item-id (:route-params req)))
        checked (get-in req [:params "checked"])
        exists? (update-item db item-id (= "true" checked))]
    (if exists?
      {:status 302
       :headers {"Location" "/items"}
       :body ""}
      {:status 404
       :body "List not found"
       :headers {}})))