function getEnumKeyByValue(enumObj: object, value: number | string): string | undefined {
  return Object.keys(enumObj).find((key) => enumObj[key as keyof typeof enumObj] === value);
}