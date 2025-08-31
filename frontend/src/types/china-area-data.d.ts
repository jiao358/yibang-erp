declare module 'china-area-data' {
  interface AreaData {
    [code: string]: string
  }
  
  export const pcaa: {
    [code: string]: AreaData
  }
}
