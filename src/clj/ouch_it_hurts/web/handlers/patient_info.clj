(ns ouch-it-hurts.web.handlers.patient-info
  (:require
   [ouch-it-hurts.patients-info.service :as s]
   [ouch-it-hurts.web.http-responses.core :as http-resp]))

(defn- get-all [{{{:keys [offset limit] :as query-params} :query-params} :app/request}]
  (-> (s/get-all query-params)
      (http-resp/json-ok)))

(defn- add-new [{{:keys [body]} :app/request}]
  (-> (s/add-patient-info body)
      (http-resp/json-ok)))

(defn- get-by-id [{{{:keys [id]} :path-params} :app/request}]
  (-> (s/get-by-id (parse-long id))
      (http-resp/json-ok)))

(defn- update-info [{{body :body {:keys [id]} :path-params} :app/request}]
  (-> (s/update-patient-info (parse-long id) body)
      (http-resp/json-ok)))


(defn- delete [{{{:keys [id]} :path-params} :app/request}]
  (-> (s/delete-patient-info (parse-long id))
      (http-resp/json-ok)))

(defn routes []
  [
   ["/patients" {:get {:handler get-all}
                 :post {:handler add-new}}]
   ["/patient/:id" {:get {:handler get-by-id}
                    :put {:handler update-info}
                    :delete {:handler delete}}]
   ])
