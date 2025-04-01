export interface MenuItem {
  label: string
  action: () => Promise<void>
}
