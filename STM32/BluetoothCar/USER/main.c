#include "io.h"
#include "delay.h"
#include "sys.h"
#include "usart.h"
#include <string.h>

void set(int a)
{
	IO1 = (a & 8) / 8;
	IO2 = (a & 4) / 4;
	IO3 = (a & 2) / 2;
	IO4 = (a & 1) / 1;
}

int main(void)
{
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	uart_init(9600);
	LED_Init();
	
	LED13=0;
	while (1)
	{
		switch (USART_RX_BUF)
    {
      case ('A'):
        set(0x2);
        break;
      case ('B'):
        set(0xa);
        break;
      case ('C'):
        set(0x8);
        break;
      case ('D'):
        set(0x6);
        break;
      case ('E'):
        set(0xf);
        break;
      case ('F'):
        set(0x9);
        break;
      case ('G'):
        set(0x4);
        break;
      case ('H'):
        set(0x5);
        break;
      case ('I'):
        set(0x1);
        break;
      default:
				set(0xf);
        break;
    }
	}
}
