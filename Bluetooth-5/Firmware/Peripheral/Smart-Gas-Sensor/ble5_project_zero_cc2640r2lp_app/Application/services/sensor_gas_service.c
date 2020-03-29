/**********************************************************************************************
 * Filename:       sensor_gas_service.c
 *
 * Description:    This file contains the implementation of the service.
 *
 * Copyright (c) 2015-2019, Texas Instruments Incorporated
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * *  Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *  Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * *  Neither the name of Texas Instruments Incorporated nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *************************************************************************************************/
/**********************************************************************************************
 Autor: Jorge Bianchetti
 Data:  01/2020

 Código: https://github.com/jorgebianchetti/tg-iar-smart-home 
*************************************************************************************************/

/*********************************************************************
 * INCLUDES
 */
#include <string.h>

#include <icall.h>

/* This Header file contains all BLE API and icall structure definition */
#include "icall_ble_api.h"

#include "sensor_gas_service.h"

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * CONSTANTS
 */

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
* GLOBAL VARIABLES
*/

// sensor_gas_service Service UUID
CONST uint8_t sensor_gas_serviceUUID[ATT_BT_UUID_SIZE] =
    {
        LO_UINT16(SENSOR_GAS_SERVICE_SERV_UUID), HI_UINT16(SENSOR_GAS_SERVICE_SERV_UUID)};

// detection UUID
CONST uint8_t sensor_gas_service_DetectionUUID[ATT_BT_UUID_SIZE] =
    {
        LO_UINT16(SENSOR_GAS_SERVICE_DETECTION_UUID), HI_UINT16(SENSOR_GAS_SERVICE_DETECTION_UUID)};

/*********************************************************************
 * LOCAL VARIABLES
 */

static sensor_gas_serviceCBs_t *pAppCBs = NULL;

/*********************************************************************
* Profile Attributes - variables
*/

// Service declaration
static CONST gattAttrType_t sensor_gas_serviceDecl = {ATT_BT_UUID_SIZE, sensor_gas_serviceUUID};

// Characteristic "Detection" Properties (for declaration)
static uint8_t sensor_gas_service_DetectionProps = GATT_PROP_READ | GATT_PROP_NOTIFY;

// Characteristic "Detection" Value variable
static uint8_t sensor_gas_service_DetectionVal[SENSOR_GAS_SERVICE_DETECTION_LEN] = {0};

// Characteristic "Detection" CCCD
static gattCharCfg_t *sensor_gas_service_DetectionConfig;

/*********************************************************************
* Profile Attributes - Table
*/

static gattAttribute_t sensor_gas_serviceAttrTbl[] =
    {
        // sensor_gas_service Service Declaration
        {
            {ATT_BT_UUID_SIZE, primaryServiceUUID},
            GATT_PERMIT_READ,
            0,
            (uint8_t *)&sensor_gas_serviceDecl},
        // Detection Characteristic Declaration
        {
            {ATT_BT_UUID_SIZE, characterUUID},
            GATT_PERMIT_READ,
            0,
            &sensor_gas_service_DetectionProps},
        // Detection Characteristic Value
        {
            {ATT_BT_UUID_SIZE, sensor_gas_service_DetectionUUID},
            GATT_PERMIT_READ,
            0,
            sensor_gas_service_DetectionVal},
        // Detection CCCD
        {
            {ATT_BT_UUID_SIZE, clientCharCfgUUID},
            GATT_PERMIT_READ | GATT_PERMIT_WRITE,
            0,
            (uint8 *)&sensor_gas_service_DetectionConfig},
};

/*********************************************************************
 * LOCAL FUNCTIONS
 */
static bStatus_t sensor_gas_service_ReadAttrCB(uint16_t connHandle, gattAttribute_t *pAttr,
                                               uint8_t *pValue, uint16_t *pLen, uint16_t offset,
                                               uint16_t maxLen, uint8_t method);
static bStatus_t sensor_gas_service_WriteAttrCB(uint16_t connHandle, gattAttribute_t *pAttr,
                                                uint8_t *pValue, uint16_t len, uint16_t offset,
                                                uint8_t method);

/*********************************************************************
 * PROFILE CALLBACKS
 */
// Simple Profile Service Callbacks
CONST gattServiceCBs_t sensor_gas_serviceCBs =
    {
        sensor_gas_service_ReadAttrCB,  // Read callback function pointer
        sensor_gas_service_WriteAttrCB, // Write callback function pointer
        NULL                            // Authorization callback function pointer
};

/*********************************************************************
* PUBLIC FUNCTIONS
*/

/*
 * Sensor_gas_service_AddService- Initializes the Sensor_gas_service service by registering
 *          GATT attributes with the GATT server.
 *
 */
extern bStatus_t Sensor_gas_service_AddService(uint8_t rspTaskId)
{
  uint8_t status;

  // Allocate Client Characteristic Configuration table
  sensor_gas_service_DetectionConfig = (gattCharCfg_t *)ICall_malloc(sizeof(gattCharCfg_t) * linkDBNumConns);
  if (sensor_gas_service_DetectionConfig == NULL)
  {
    return (bleMemAllocError);
  }

  // Initialize Client Characteristic Configuration attributes
  GATTServApp_InitCharCfg(CONNHANDLE_INVALID, sensor_gas_service_DetectionConfig);
  // Register GATT attribute list and CBs with GATT Server App
  status = GATTServApp_RegisterService(sensor_gas_serviceAttrTbl,
                                       GATT_NUM_ATTRS(sensor_gas_serviceAttrTbl),
                                       GATT_MAX_ENCRYPT_KEY_SIZE,
                                       &sensor_gas_serviceCBs);

  return (status);
}

/*
 * Sensor_gas_service_RegisterAppCBs - Registers the application callback function.
 *                    Only call this function once.
 *
 *    appCallbacks - pointer to application callbacks.
 */
bStatus_t Sensor_gas_service_RegisterAppCBs(sensor_gas_serviceCBs_t *appCallbacks)
{
  if (appCallbacks)
  {
    pAppCBs = appCallbacks;

    return (SUCCESS);
  }
  else
  {
    return (bleAlreadyInRequestedMode);
  }
}

/*
 * Sensor_gas_service_SetParameter - Set a Sensor_gas_service parameter.
 *
 *    param - Profile parameter ID
 *    len - length of data to right
 *    value - pointer to data to write.  This is dependent on
 *          the parameter ID and WILL be cast to the appropriate
 *          data type (example: data type of uint16 will be cast to
 *          uint16 pointer).
 */
bStatus_t Sensor_gas_service_SetParameter(uint8_t param, uint16_t len, void *value)
{
  bStatus_t ret = SUCCESS;
  switch (param)
  {
  case SENSOR_GAS_SERVICE_DETECTION_ID:
    if (len == SENSOR_GAS_SERVICE_DETECTION_LEN)
    {
      memcpy(sensor_gas_service_DetectionVal, value, len);

      // Try to send notification.
      GATTServApp_ProcessCharCfg(sensor_gas_service_DetectionConfig, (uint8_t *)&sensor_gas_service_DetectionVal, FALSE,
                                 sensor_gas_serviceAttrTbl, GATT_NUM_ATTRS(sensor_gas_serviceAttrTbl),
                                 INVALID_TASK_ID, sensor_gas_service_ReadAttrCB);
    }
    else
    {
      ret = bleInvalidRange;
    }
    break;

  default:
    ret = INVALIDPARAMETER;
    break;
  }
  return ret;
}

/*
 * Sensor_gas_service_GetParameter - Get a Sensor_gas_service parameter.
 *
 *    param - Profile parameter ID
 *    value - pointer to data to write.  This is dependent on
 *          the parameter ID and WILL be cast to the appropriate
 *          data type (example: data type of uint16 will be cast to
 *          uint16 pointer).
 */
bStatus_t Sensor_gas_service_GetParameter(uint8_t param, uint16_t *len, void *value)
{
  bStatus_t ret = SUCCESS;
  switch (param)
  {
  default:
    ret = INVALIDPARAMETER;
    break;
  }
  return ret;
}

/*********************************************************************
 * @fn          sensor_gas_service_ReadAttrCB
 *
 * @brief       Read an attribute.
 *
 * @param       connHandle - connection message was received on
 * @param       pAttr - pointer to attribute
 * @param       pValue - pointer to data to be read
 * @param       pLen - length of data to be read
 * @param       offset - offset of the first octet to be read
 * @param       maxLen - maximum length of data to be read
 * @param       method - type of read message
 *
 * @return      SUCCESS, blePending or Failure
 */
static bStatus_t sensor_gas_service_ReadAttrCB(uint16_t connHandle, gattAttribute_t *pAttr,
                                               uint8_t *pValue, uint16_t *pLen, uint16_t offset,
                                               uint16_t maxLen, uint8_t method)
{
  bStatus_t status = SUCCESS;

  // See if request is regarding the Detection Characteristic Value
  if (!memcmp(pAttr->type.uuid, sensor_gas_service_DetectionUUID, pAttr->type.len))
  {
    if (offset > SENSOR_GAS_SERVICE_DETECTION_LEN) // Prevent malicious ATT ReadBlob offsets.
    {
      status = ATT_ERR_INVALID_OFFSET;
    }
    else
    {
      *pLen = MIN(maxLen, SENSOR_GAS_SERVICE_DETECTION_LEN - offset); // Transmit as much as possible
      memcpy(pValue, pAttr->pValue + offset, *pLen);
    }
  }
  else
  {
    // If we get here, that means you've forgotten to add an if clause for a
    // characteristic value attribute in the attribute table that has READ permissions.
    *pLen = 0;
    status = ATT_ERR_ATTR_NOT_FOUND;
  }

  return status;
}

/*********************************************************************
 * @fn      sensor_gas_service_WriteAttrCB
 *
 * @brief   Validate attribute data prior to a write operation
 *
 * @param   connHandle - connection message was received on
 * @param   pAttr - pointer to attribute
 * @param   pValue - pointer to data to be written
 * @param   len - length of data
 * @param   offset - offset of the first octet to be written
 * @param   method - type of write message
 *
 * @return  SUCCESS, blePending or Failure
 */
static bStatus_t sensor_gas_service_WriteAttrCB(uint16_t connHandle, gattAttribute_t *pAttr,
                                                uint8_t *pValue, uint16_t len, uint16_t offset,
                                                uint8_t method)
{
  bStatus_t status = SUCCESS;
  uint8_t paramID = 0xFF;

  // See if request is regarding a Client Characterisic Configuration
  if (!memcmp(pAttr->type.uuid, clientCharCfgUUID, pAttr->type.len))
  {
    // Allow only notifications.
    status = GATTServApp_ProcessCCCWriteReq(connHandle, pAttr, pValue, len,
                                            offset, GATT_CLIENT_CFG_NOTIFY);
  }
  else
  {
    // If we get here, that means you've forgotten to add an if clause for a
    // characteristic value attribute in the attribute table that has WRITE permissions.
    status = ATT_ERR_ATTR_NOT_FOUND;
  }

  // Let the application know something changed (if it did) by using the
  // callback it registered earlier (if it did).
  if (paramID != 0xFF)
    if (pAppCBs && pAppCBs->pfnChangeCb)
      pAppCBs->pfnChangeCb(connHandle, paramID, len, pValue); // Call app function from stack task context.

  return status;
}
