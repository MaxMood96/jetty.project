//
// ========================================================================
// Copyright (c) 1995-2021 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.util.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Password utility class.
 *
 * This utility class gets a password or pass phrase either by:
 *
 * <PRE>
 * + Password is set as a system property.
 * + The password is prompted for and read from standard input
 * + A program is run to get the password.
 * </pre>
 *
 * Passwords that begin with OBF: are de obfuscated. Passwords can be obfuscated
 * by run org.eclipse.util.Password as a main class. Obfuscated password are
 * required if a system needs to recover the full password (eg. so that it may
 * be passed to another system). They are not secure, but prevent casual
 * observation.
 * <p>
 * Passwords that begin with CRYPT: are oneway encrypted with UnixCrypt. The
 * real password cannot be retrieved, but comparisons can be made to other
 * passwords. A Crypt can be generated by running org.eclipse.util.UnixCrypt as
 * a main class, passing password and then the username. Checksum passwords are
 * a secure(ish) way to store passwords that only need to be checked rather than
 * recovered. Note that it is not strong security - specially if simple
 * passwords are used.
 */
public class Password extends Credential
{
    private static final Logger LOG = LoggerFactory.getLogger(Password.class);

    private static final long serialVersionUID = 5062906681431569445L;

    public static final String __OBFUSCATE = "OBF:";

    private String _pw;

    /**
     * Constructor.
     *
     * @param password The String password.
     */
    public Password(String password)
    {
        _pw = password;

        // expand password
        while (_pw != null && _pw.startsWith(__OBFUSCATE))
        {
            _pw = deobfuscate(_pw);
        }
    }

    @Override
    public String toString()
    {
        return _pw;
    }

    public String toStarString()
    {
        return "*****************************************************".substring(0, _pw.length());
    }

    @Override
    public boolean check(Object credentials)
    {
        if (this == credentials)
            return true;

        if (credentials instanceof Password)
            return credentials.equals(_pw);

        if (credentials instanceof String)
            return stringEquals(_pw, (String)credentials);

        if (credentials instanceof char[])
            return stringEquals(_pw, new String((char[])credentials));

        if (credentials instanceof Credential)
            return ((Credential)credentials).check(_pw);

        return false;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (null == o)
            return false;

        if (o instanceof Password)
            return stringEquals(_pw, ((Password)o)._pw);

        if (o instanceof String)
            return stringEquals(_pw, (String)o);

        return false;
    }

    @Override
    public int hashCode()
    {
        return null == _pw ? super.hashCode() : _pw.hashCode();
    }

    public static String obfuscate(String s)
    {
        StringBuilder buf = new StringBuilder();
        byte[] b = s.getBytes(StandardCharsets.UTF_8);

        buf.append(__OBFUSCATE);
        for (int i = 0; i < b.length; i++)
        {
            byte b1 = b[i];
            byte b2 = b[b.length - (i + 1)];
            if (b1 < 0 || b2 < 0)
            {
                int i0 = (0xff & b1) * 256 + (0xff & b2);
                String x = Integer.toString(i0, 36).toLowerCase(Locale.ENGLISH);
                buf.append("U0000", 0, 5 - x.length());
                buf.append(x);
            }
            else
            {
                int i1 = 127 + b1 + b2;
                int i2 = 127 + b1 - b2;
                int i0 = i1 * 256 + i2;
                String x = Integer.toString(i0, 36).toLowerCase(Locale.ENGLISH);

                int j0 = Integer.parseInt(x, 36);
                int j1 = (i0 / 256);
                int j2 = (i0 % 256);
                byte bx = (byte)((j1 + j2 - 254) / 2);

                buf.append("000", 0, 4 - x.length());
                buf.append(x);
            }
        }
        return buf.toString();
    }

    public static String deobfuscate(String s)
    {
        if (s.startsWith(__OBFUSCATE))
            s = s.substring(4);

        byte[] b = new byte[s.length() / 2];
        int l = 0;
        for (int i = 0; i < s.length(); i += 4)
        {
            if (s.charAt(i) == 'U')
            {
                i++;
                String x = s.substring(i, i + 4);
                int i0 = Integer.parseInt(x, 36);
                byte bx = (byte)(i0 >> 8);
                b[l++] = bx;
            }
            else
            {
                String x = s.substring(i, i + 4);
                int i0 = Integer.parseInt(x, 36);
                int i1 = (i0 / 256);
                int i2 = (i0 % 256);
                byte bx = (byte)((i1 + i2 - 254) / 2);
                b[l++] = bx;
            }
        }

        return new String(b, 0, l, StandardCharsets.UTF_8);
    }

    /**
     * Get a password. A password is obtained by trying
     * <UL>
     * <LI>Calling <Code>System.getProperty(realm,dft)</Code>
     * <LI>Prompting for a password
     * <LI>Using promptDft if nothing was entered.
     * </UL>
     *
     * @param realm The realm name for the password, used as a SystemProperty
     * name.
     * @param dft The default password.
     * @param promptDft The default to use if prompting for the password.
     * @return Password
     */
    public static Password getPassword(String realm, String dft, String promptDft)
    {
        String passwd = System.getProperty(realm, dft);
        if (passwd == null || passwd.length() == 0)
        {
            try
            {
                System.out.print(realm + ((promptDft != null && promptDft.length() > 0) ? " [dft]" : "") + " : ");
                System.out.flush();
                byte[] buf = new byte[512];
                int len = System.in.read(buf);
                if (len > 0)
                    passwd = new String(buf, 0, len).trim();
            }
            catch (IOException e)
            {
                LOG.warn("EXCEPTION", e);
            }
            if (passwd == null || passwd.length() == 0)
                passwd = promptDft;
        }
        return new Password(passwd);
    }

    public static void main(String[] arg)
    {
        if (arg.length != 1 && arg.length != 2)
        {
            System.err.println("Usage - java " + Password.class.getName() + " [<user>] <password>");
            System.err.println("If the password is ?, the user will be prompted for the password");
            System.exit(1);
        }
        String p = arg[arg.length == 1 ? 0 : 1];
        Password pw = new Password(p);
        System.err.println(pw.toString());
        System.err.println(obfuscate(pw.toString()));
        System.err.println(Credential.MD5.digest(p));
        if (arg.length == 2)
            System.err.println(Credential.Crypt.crypt(arg[0], pw.toString()));
    }
}
