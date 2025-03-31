export interface MenuItem {
  name: string
  action: () => Promise<void>
}
