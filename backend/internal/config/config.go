package config

type Root struct {
	Logging  Logging  `yaml:"logging"`
	DB       DB       `yaml:"db"`
	Redis    Redis    `yaml:"redis"`
	Mail     Mail     `yaml:"mail"`
	Security Security `yaml:"security"`
}

type Logging struct {
	Level string `yaml:"level"`
}

type DB struct {
	Url      string `yaml:"url"`
	Username string `yaml:"username"`
	Password string `yaml:"password"`
}

type Redis struct {
	Host     string `yaml:"host"`
	Port     int    `yaml:"port"`
	Password string `yaml:"password"`
	Timeout  int    `yaml:"timeout"`
}

type Mail struct {
	Host     string `yaml:"host"`
	Port     int    `yaml:"port"`
	Username string `yaml:"username"`
	Password string `yaml:"password"`
	From     string `yaml:"from"`
}

type Security struct {
	AccessTokenTTL  int    `yaml:"access_token_ttl"`
	RefreshTokenTTL int    `yaml:"refresh_token_ttl"`
	Secret          string `yaml:"secret"`
}
