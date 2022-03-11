#ifndef __USART2_H
#define __USART2_H	 

#include "sys.h"
#include <stdarg.h>
#include <stdio.h>
#include <string.h>

//?USART2?????????
//**********************************************************************************
#define RCC_USART  RCC_APB2Periph_GPIOA
#define RCC_TX  RCC_APB2Periph_GPIOA
#define RCC_RX RCC_APB2Periph_GPIOA
#define USART USART2
#define USART_TX_Pin GPIO_Pin_2; //USART2_TX   PA.2
#define USART_RX_Pin GPIO_Pin_3; //USART2_RX   PA.3
//**********************************************************************************

#define USART2_MAX_RECV_LEN		200					//?????????
#define USART2_MAX_SEND_LEN		200					//?????????


#define USART2_RX_EN 			1					//0,???;1,??.

extern u8  USART2_RX_BUF[USART2_MAX_RECV_LEN]; 		//????,??USART2_MAX_RECV_LEN??
extern u8  USART2_TX_BUF[USART2_MAX_SEND_LEN]; 		//????,??USART2_MAX_SEND_LEN??
extern u16 USART2_RX_STA;   						//??????

void TIM4_Set(u8 sta);
void TIM4_Init(u16 arr,u16 psc);
void UART_DMA_Config(DMA_Channel_TypeDef*DMA_CHx,u32 cpar,u32 cmar);
void UART_DMA_Enable(DMA_Channel_TypeDef*DMA_CHx,u8 len);
void u2_printf(char* fmt, ...);

#endif
