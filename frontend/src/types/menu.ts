export interface MenuItem {
  label: string
  action?: () => Promise<void>
  children?: MenuItems
  disabled?: boolean
}

export interface MenuItems {
  [key: string] : MenuItem
}
