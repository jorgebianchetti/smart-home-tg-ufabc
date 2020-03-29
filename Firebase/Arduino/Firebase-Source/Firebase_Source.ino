/************************************************************************************/
/* Trabalho de Graduação do curso Engenharia de Instrumentação, Automação e Robótica
/* Universidade Federal do ABC
/*
/* Código em Arduino para a plataforma NodeMCU para conexão com WiFi, comunição com
/* o módulo BLE 5 CC2640R2F e comunição com banco de dados Firebase
/*
/* Autor: Jorge Bianchetti
/* Data:  01/2020
/*
/* Código: https://github.com/jorgebianchetti/tg-iar-smart-home
/************************************************************************************/

/* INCLUDES */
#include <WiFiManager.h>
#include <FirebaseArduino.h>

/* DEFINES */
// Pinos utilizados para interrupção
#define RX_PIN 5 // Interrupçao vinda do BLE central
#define TX_PIN 4 // Interrupção enviada ao BLE central

// LEDs
#define YELLOW_LED 14 // Informa status da conexão com o WiFi

// Botões
#define RESET_CONN_BTN 12 // Botão para resetar as informações utilizadas para conexão com WiFi

// Dados de autenticação do Firebase
#define FIREBASE_HOST "tg-iar-firebase.firebaseio.com"           // URL do banco de dados
#define FIREBASE_AUTH "UT5hneBy82MCOH675qp7PqntwxB32bBKJJdpBwwP" // Chave de acesso do banco de dados

// Buffer de comunicação com o BLE central
#define BUF_SIZE 5        // Tamanho do buffer
#define BUF_CMD 0         // Código do comando
#define BUF_NUM 1         // Número do dispositivo
#define BUF_VAL1 2        // Primeiro valor
#define BUF_VAL2 3        // Segundo valor
#define BUF_END 4         // Fim do buffer
#define BUF_END_CHAR 0x0A // Quebra de linha

// Tipos de comando
#define CMD_BLE 0x30  // BLE central envia mensagem ao NodeMCU
#define CMD_NODE 0x31 // NodeMCU envia mensagem ao BLE central

// Números dos periféricos
#define TOMADA_NUM 0x00     // Tomada Inteligente
#define SENSOR_GAS_NUM 0x01 // Sensor de Gás

/* VARIÁVEIS GLOBAIS */
int n = 1;
volatile bool intFlag = 0;
volatile uint32_t intDebounce = 0;
uint8_t rxBuf[BUF_SIZE];
uint8_t txBuf[BUF_SIZE];
char spf[60];
bool shouldSaveConfig = false;
char firebaseUser[40];
char firebasePassword[40];

/* FUNÇÕES */
void updateFBFromHub(uint8_t *buf); // Atualiza o banco de dados Firebase com os novos valores
void saveConfigCallback();          // Salva as configurações de inicialização do WiFi
void setupSpiffs();                 // Configura o arquivo com as informações do usuário

void setup()
{
  // Inicialização dos GPIOs
  pinMode(YELLOW_LED, OUTPUT);
  digitalWrite(YELLOW_LED, LOW);

  pinMode(TX_PIN, OUTPUT);
  digitalWrite(TX_PIN, HIGH);

  pinMode(RESET_CONN_BTN, INPUT_PULLUP);

  // Inicialização da UART
  Serial.begin(115200); // Baudrate de 115200
  Serial.swap();        // Pino TX  ->  D8
                        // Pino RX  ->  D7

  // Inicialização da conexão WiFi
  WiFi.mode(WIFI_STA); // Modo "station"

  setupSpiffs(); // Checa se há configuração de WiFi prévia

  WiFiManager wm;
  wm.setDebugOutput(false);                     // Desabilita o log do WiFi Manager
  wm.setSaveConfigCallback(saveConfigCallback); // Seta a função de "callback" para salvar as informações do WiFi

  WiFiManagerParameter custom_firebase_user("user", "Account username", "", 40);
  WiFiManagerParameter custom_firebase_password("password", "Account password", "", 40);

  wm.addParameter(&custom_firebase_user);     // Adiciona campo para introduzir o nome de usuário
  wm.addParameter(&custom_firebase_password); // Adiciona campo para introduzir a senha

  if (!digitalRead(RESET_CONN_BTN)) // Caso o botão de reset esteja pressionado
  {
    for (uint8_t flashes = 0; flashes < 5; flashes++)
    {
      digitalWrite(YELLOW_LED, HIGH);
      delay(200);
      digitalWrite(YELLOW_LED, LOW);
      delay(200);
    }
    wm.resetSettings(); // entra no modo de reinicialização das configurações
  }

  while (!wm.autoConnect("HUB Login")) // Caso conexão não seja estabelecida, reinicia o dispositivo
  {
    delay(3000);
    ESP.restart();
    delay(5000);
  }

  // Caso seja necessário, inicia o processo de salvamento das informações para comunicação com o Firebase,
  // no formato JSON, em um sistema de arquivos interno SPIFFS
  if (shouldSaveConfig)
  {
    strcpy(firebaseUser, custom_firebase_user.getValue());
    strcpy(firebasePassword, custom_firebase_password.getValue());

    DynamicJsonBuffer jsonBuffer;
    JsonObject &json = jsonBuffer.createObject();
    json["firebase_user"] = firebaseUser;
    json["firebase_password"] = firebasePassword;

    File configFile = SPIFFS.open("/config.json", "w");
    if (!configFile)
    {
    }

    json.printTo(configFile);
    configFile.close();

    shouldSaveConfig = false;
  }

  delay(2000);

  // Inicialização da comunicação com o banco de dados Firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH); // Inicia a conexão
  if (Firebase.failed())                        // Caso falhe, entra em "loop" piscando o LED amarelo
  {
    while (1)
    {
      digitalWrite(YELLOW_LED, HIGH);
      delay(1000);
      digitalWrite(YELLOW_LED, LOW);
      delay(1000);
    }
  }

  String checkPassword = Firebase.getString("Users/" + String(firebaseUser) + "/password");
  if (checkPassword.compareTo(String(firebasePassword)) != 0) // Checa se a senha introduzida pelo usuário corresponde ao usuário informado
  {
    while (1) // Caso falhe, entra em "loop" piscando o LED amarelo
    {
      digitalWrite(YELLOW_LED, HIGH);
      delay(1000);
      digitalWrite(YELLOW_LED, LOW);
      delay(1000);
    }
  }

  Firebase.stream("");   // Entra no modo "stream" para receber informações do Firebase
  if (Firebase.failed()) // Caso falhe, entra em "loop" piscando o LED amarelo
  {
    while (1)
    {
      digitalWrite(YELLOW_LED, HIGH);
      delay(1000);
      digitalWrite(YELLOW_LED, LOW);
      delay(1000);
    }
  }

  int time = millis();
  while (millis() - time < 2000) // Limpa o "buffer" da UART
  {
    Serial.read();
    delay(1);
  }

  digitalWrite(YELLOW_LED, HIGH); // Liga o LED amarelo

  attachInterrupt(digitalPinToInterrupt(RX_PIN), receiveRx, FALLING); // Inicia a interrupção do pino RX_PIN passando a função receiveRx()

  delay(30);
  intFlag = false; // Ignora qualquer possível interrupção até este ponto
}

void loop()
{
  if (intFlag) // Caso receber interrupção do BLE central
  {
    delay(10);
    Serial.readBytes(rxBuf, 5);    // Lê o "buffer" recebido
    if (rxBuf[BUF_CMD] == CMD_BLE) // Caso a mensagem seja válida, atualiza o banco de dados
      updateFBFromHub(rxBuf);

    int time = millis();
    while (millis() - time < 50) // Limpa o "buffer" da UART
    {
      Serial.read();
      delay(1);
    }

    intFlag = false;
  }

  if (Firebase.available()) // Caso haja atualização vinda do banco de dados Firebase
  {
    FirebaseObject event = Firebase.readEvent(); // Lê a mensagem

    int eventData = event.getInt("data"); // Lê o novo valor

    String eventPath = event.getString("path"); // Lê o caminho
    eventPath.toLowerCase();

    char eventPathBuf[50];
    eventPath.toCharArray(eventPathBuf, 50);

    if (eventPath == "" || eventPath == "/") // Caso a mensagem não tenha relevância, ignora
    {
    }
    else
    {
      strtok(eventPathBuf, "/");
      if (strcmp(strtok(NULL, "/"), firebaseUser) == 0) // Checa se a mensagem é do usuário cadastrado
      {
        if (strcmp(strtok(NULL, "/"), "powerplug") == 0) // Checa se a mensagem é referente à Tomada Inteligente
        {
          txBuf[BUF_CMD] = CMD_NODE;
          txBuf[BUF_NUM] = TOMADA_NUM;
          txBuf[BUF_VAL1] = eventData;
          txBuf[BUF_VAL2] = 0x00;
          txBuf[BUF_END] = BUF_END_CHAR;

          // Envia mensagem ao BLE central com a atualização
          Serial.write(txBuf, 5);
          delay(10);
          digitalWrite(TX_PIN, LOW);
          delay(1);
          digitalWrite(TX_PIN, HIGH);
        }
        else if (strcmp(strtok(NULL, "/"), "gassensor") == 0) // Checa se a mensagem é referente ao Sensor de Gás
        {
          txBuf[BUF_CMD] = CMD_NODE;
          txBuf[BUF_NUM] = TOMADA_NUM;
          txBuf[BUF_VAL1] = eventData;
          txBuf[BUF_VAL2] = 0x00;
          txBuf[BUF_END] = BUF_END_CHAR;

          // Envia mensagem ao BLE central com a atualização
          Serial.write(txBuf, 5);
          delay(10);
          digitalWrite(TX_PIN, LOW);
          delay(1);
          digitalWrite(TX_PIN, HIGH);
        }
      }
    }
  }
}

void updateFBFromHub(uint8_t *buf)
{
  if (buf[BUF_NUM] == TOMADA_NUM) // Caso a atualização seja para a Tomada Inteligente
  {
    Firebase.setInt("Devices/" + String(firebaseUser) + "/powerplug/value", buf[BUF_VAL1]); // Seta o novo valor
    yield();
    if (Firebase.failed())
    {
    }
  }

  if (buf[BUF_NUM] == SENSOR_GAS_NUM) // Caso a atualização seja para o Sensor de Gás
  {
    Serial.println("gas");
    Firebase.setInt("Devices/" + String(firebaseUser) + "/gassensor/value", buf[BUF_VAL1]); // Seta o novo valor
    yield();
    if (Firebase.failed())
    {
    }
  }
}

void saveConfigCallback()
{
  shouldSaveConfig = true;
}

void setupSpiffs()
{
  if (SPIFFS.begin()) // Inicializa a comunicação com o sistema de arquivos SPIFFS
  {
    if (SPIFFS.exists("/config.json")) // Checa se existe o arquivo "config.json"
    {
      File configFile = SPIFFS.open("/config.json", "r"); // Abre o arquivo
      if (configFile)
      {
        size_t size = configFile.size();
        std::unique_ptr<char[]> buf(new char[size]);

        configFile.readBytes(buf.get(), size); // Lê os dados
        DynamicJsonBuffer jsonBuffer;
        JsonObject &json = jsonBuffer.parseObject(buf.get());
        if (json.success())
        {
          strcpy(firebaseUser, json["firebase_user"]);         // Copia o nome de usuário
          strcpy(firebasePassword, json["firebase_password"]); // Copia a senha
        }
        else
        {
        }
      }
    }
    else
    {
    }
  }
  else
  {
  }
}

ICACHE_RAM_ATTR void receiveRx(void)
{
  if (millis() - intDebounce >= 50) // Debounce para o pino de interrupção
  {
    if (!intFlag)
      intFlag = true;

    intDebounce = millis();
  }
}