#!/usr/bin/env bash

configure_database() {
  echo "Configuring timi::database"

  if [[ ! -e "${TIMI_DATA_DIR}/timi.db" ]]; then
    echo "Generating timi::database in ${TIMI_DATA_DIR}/timi.db"

    mkdir -p ${TIMI_DATA_DIR}
    lein run sqlite create-db :filename "${TIMI_DATA_DIR}/timi.db"
  fi

  if [[ ! -e "${TIMI_DATA_DIR}/config.edn" ]]; then
    echo "Generatinig timi::database configuration"

cat << EOF > ${TIMI_DATA_DIR}/config.edn
{:cookie-encryption-key "`openssl rand -base64 12`"
 :selmer-caching? false

 :persistence :sqlite
 :persistence-strategies {}
 :sqlite {:subprotocol "sqlite" :subname "${TIMI_DATA_DIR}/timi.db"}

 :authentication :single-user
 :single-user {:username "me!"}}
EOF
  fi

  echo "Copying timi::database configuration from ${TIMI_DATA_DIR}/config.edn"
  cp "${TIMI_DATA_DIR}/config.edn" "${TIMI_INSTALL_DIR}/config/local/config.edn"
}


case ${1} in
  app:start|app:projects|app:tasks|app:create-overhead-task)

    configure_database

    case ${1} in
      app:start)
        lein with-profile local ring server-headless
        ;;
      app:projects)
        shift 1
        lein with-profile local run projects $@
        ;;
      app:tasks)
        shift 1
        lein with-profile local run tasks $@
        ;;
    esac
    ;;
  app:help)
    echo "Available options:"
    echo " app:start                - Starts the Tími server (default)"
    echo " app:projects             - Run project action"
    echo " app:tasks                - Run task action"
    echo " app:help                 - Displays the help"
    echo " [command]                - Execute the specified command, eg. bash."
    ;;
  *)
    exec "$@"
    ;;
esac
