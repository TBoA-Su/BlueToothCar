#ifndef __USART_H
#define __USART_H
#include "stdio.h"	
#include "sys.h" 

extern u8  USART_RX_BUF; //接收缓冲,最大1个字节.

void uart_init(u32 bound);

#endif

