#include "usart2.h"


//??????? 	
__align(8) u8 USART2_TX_BUF[USART2_MAX_SEND_LEN]; 	//????,??USART2_MAX_SEND_LEN??
#ifdef USART2_RX_EN   								//???????   	  
//??????? 	
u8 USART2_RX_BUF[USART2_MAX_RECV_LEN]; 				//????,??USART2_MAX_RECV_LEN???.


//????????2????????????10ms?????????????.
//??2?????????10ms,?????1?????.?????10ms?????
//????,?????????.
//????????
//[15]:0,???????;1,????????.
//[14:0]:????????
u16 USART2_RX_STA=0;   	
void USART2_IRQHandler(void)
{
	u8 res;	    
	if(USART_GetITStatus(USART2, USART_IT_RXNE) != RESET)//?????
	{	 
 
	res =USART_ReceiveData(USART2);		
		if(USART2_RX_STA<USART2_MAX_RECV_LEN)		//???????
		{
			TIM_SetCounter(TIM4,0);//?????        				 
			if(USART2_RX_STA==0)TIM4_Set(1);	 	//?????4??? 
			USART2_RX_BUF[USART2_RX_STA++]=res;		//???????	 
		}else 
		{
			USART2_RX_STA|=1<<15;					//????????
		} 
	}  											 
}   

//???USART2
//bound:???	 
void USART2_Init(u32 bound)
{
	GPIO_InitTypeDef GPIO_InitStructure;
	USART_InitTypeDef USART_InitStructure;
	NVIC_InitTypeDef NVIC_InitStructure;
	
	RCC_APB2PeriphClockCmd(RCC_USART,ENABLE);
	RCC_APB2PeriphClockCmd(RCC_TX,ENABLE);
	RCC_APB2PeriphClockCmd(RCC_RX,ENABLE);

	
	USART_DeInit(USART);  //????
	
	
	
	//UART2_TX PA.2
	GPIO_InitStructure.GPIO_Pin=USART_TX_Pin;
	GPIO_InitStructure.GPIO_Speed=GPIO_Speed_50MHz;
	GPIO_InitStructure.GPIO_Mode=GPIO_Mode_AF_PP;
	GPIO_Init(GPIOA,&GPIO_InitStructure);
	
	//USART2_RX   PA.3
	GPIO_InitStructure.GPIO_Pin=USART_RX_Pin;
	GPIO_InitStructure.GPIO_Speed=GPIO_Speed_50MHz;
	GPIO_InitStructure.GPIO_Mode=GPIO_Mode_IN_FLOATING; //????
	GPIO_Init(GPIOA,&GPIO_InitStructure);
	
	USART_InitStructure.USART_BaudRate = bound;//?????9600;
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;//???8?????
	USART_InitStructure.USART_StopBits = USART_StopBits_1;//?????
	USART_InitStructure.USART_Parity = USART_Parity_No;//??????
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;//????????
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;	//????
	
	USART_Init(USART,&USART_InitStructure); //?????
	
	USART_DMACmd(USART2,USART_DMAReq_Tx,ENABLE);  	//????2?DMA??
	//UART_DMA_Config(DMA1_Channel7,(u32)&USART2->DR,(u32)USART2_TX_BUF);//DMA1??7,?????2,????USART2_TX_BUF 
	USART_Cmd(USART2, ENABLE);                    //???? 
	
	#ifdef USART2_RX_EN		  	//???????
	//??????
  USART_ITConfig(USART2, USART_IT_RXNE, ENABLE);//????   
	
	NVIC_InitStructure.NVIC_IRQChannel = USART2_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=2 ;//?????2
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;		//????3
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//IRQ????
	NVIC_Init(&NVIC_InitStructure);	//??????????VIC???
	TIM4_Init(99,7199);		//10ms??
	USART2_RX_STA=0;		//??
	TIM4_Set(0);			//?????4
	#endif	 	
}

//arr:??????
//psc:??????		 
void TIM4_Init(u16 arr,u16 psc)
{	
	NVIC_InitTypeDef NVIC_InitStructure;
	TIM_TimeBaseInitTypeDef  TIM_TimeBaseStructure;

	RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM4, ENABLE); //????//TIM4????    
	
	//???TIM4???
	TIM_TimeBaseStructure.TIM_Period = arr; //???????????????????????????	
	TIM_TimeBaseStructure.TIM_Prescaler =psc; //??????TIMx???????????
	TIM_TimeBaseStructure.TIM_ClockDivision = TIM_CKD_DIV1; //??????:TDTS = Tck_tim
	TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;  //TIM??????
	TIM_TimeBaseInit(TIM4, &TIM_TimeBaseStructure); //??????????TIMx???????
 
	TIM_ITConfig(TIM4,TIM_IT_Update,ENABLE ); //?????TIM4??,??????

	 	  
	NVIC_InitStructure.NVIC_IRQChannel = TIM4_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=1 ;//?????1
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 2;		//????2
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;			//IRQ????
	NVIC_Init(&NVIC_InitStructure);	//??????????VIC???
	
}
//???4??????		    
void TIM4_IRQHandler(void)
{ 	
	if (TIM_GetITStatus(TIM4, TIM_IT_Update) != RESET)//?????
	{	 			   
		USART2_RX_STA|=1<<15;	//??????
		TIM_ClearITPendingBit(TIM4, TIM_IT_Update  );  //??TIMx??????    
		TIM4_Set(0);			//??TIM4  
	}	    
}
//??TIM4???
//sta:0,??;1,??;
void TIM4_Set(u8 sta)
{
	if(sta)
	{
       
		TIM_SetCounter(TIM4,0);//?????
		TIM_Cmd(TIM4, ENABLE);  //??TIMx	
	}else TIM_Cmd(TIM4, DISABLE);//?????4	   
}

//??2,printf ??
//???????????USART2_MAX_SEND_LEN??
void u2_printf(char* fmt,...)  
{  
	va_list ap;
	va_start(ap,fmt);
	vsprintf((char*)USART2_TX_BUF,fmt,ap);
	va_end(ap);
	while(DMA_GetCurrDataCounter(DMA1_Channel7)!=0);	//????7????   
	UART_DMA_Enable(DMA1_Channel7,strlen((const char*)USART2_TX_BUF)); 	//??dma????
}

#endif

//DMA1??????
//???????????,?????????????
//????->????/8?????/???????
//DMA_CHx:DMA??CHx
//cpar:????
//cmar:?????    
void UART_DMA_Config(DMA_Channel_TypeDef*DMA_CHx,u32 cpar,u32 cmar)
{
	DMA_InitTypeDef DMA_InitStructure;
 	RCC_AHBPeriphClockCmd(RCC_AHBPeriph_DMA1, ENABLE);	//??DMA??
  DMA_DeInit(DMA_CHx);   //?DMA???1?????????
	DMA_InitStructure.DMA_PeripheralBaseAddr = cpar;  //DMA??ADC???
	DMA_InitStructure.DMA_MemoryBaseAddr = cmar;  //DMA?????
	DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;  //??????,??????????
	DMA_InitStructure.DMA_BufferSize = 0;  //DMA???DMA?????
	DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;  //?????????
	DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;  //?????????
	DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;  //?????8?
	DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte; //?????8?
	DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;  //?????????
	DMA_InitStructure.DMA_Priority = DMA_Priority_Medium; //DMA?? x?????? 
	DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;  //DMA??x????????????
	DMA_Init(DMA_CHx, &DMA_InitStructure);  //??DMA_InitStruct?????????DMA???USART1_Tx_DMA_Channel???????	
} 
//????DMA??
void UART_DMA_Enable(DMA_Channel_TypeDef*DMA_CHx,u8 len)
{
	DMA_Cmd(DMA_CHx, DISABLE );  //?? ?????        
	DMA_SetCurrDataCounter(DMA_CHx,len);//DMA???DMA?????	
	DMA_Cmd(DMA_CHx, ENABLE);           //??DMA??
}	   